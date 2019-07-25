/*
 * Copyright 2008 Google LLC
 * Modifications copyright 2019 Leon Kiefer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.legion2.deployment_descriptor_generator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import io.github.amyassist.amy.core.di.annotation.RequireDeploymentDescriptor;

/**
 * Processes annotations that has the {@link RequireDeploymentDescriptor} meta annotation and generates the deployment
 * descriptor files described in {@link java.util.ServiceLoader}.
 * <p>
 * Processor Options:
 * <ul>
 * <li>debug - turns on debug statements</li>
 * </ul>
 * 
 * @author Leon Kiefer
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ "debug" })
@AutoService(Processor.class)
public class DeploymentDescriptionProcessor extends AbstractProcessor {

	/**
	 * Maps the class names of service provider interfaces to the class names of the concrete classes which implement
	 * them.
	 * <p>
	 * For example, {@code "com.google.apphosting.LocalRpcService" ->
	 *   "com.google.apphosting.datastore.LocalDatastoreService"}
	 */
	private Multimap<String, String> providers = HashMultimap.create();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		try {
			return this.processImpl(annotations, roundEnv);
		} catch (Exception e) {
			// We don't allow exceptions of any kind to propagate to the compiler
			StringWriter writer = new StringWriter();
			e.printStackTrace(new PrintWriter(writer));
			fatalError(writer.toString());
			return false;
		}
	}

	private boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			this.generateConfigFiles();
		} else {
			this.processAnnotations(annotations, roundEnv);
		}

		return false;
	}

	private void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends TypeElement> filteredAnnotations = new HashSet<>(annotations);
		filteredAnnotations
				.removeIf(typeElement -> typeElement.getAnnotation(RequireDeploymentDescriptor.class) == null);

		for (TypeElement annotation : filteredAnnotations) {
			processAnnotation(annotation, roundEnv);
		}
	}

	private void processAnnotation(TypeElement annotation, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
		log("Annotation: " + annotation.getQualifiedName());
		for (Element e : elements) {
			TypeElement providerImplementer = (TypeElement) e;
			log("Class: " + providerImplementer.getQualifiedName());
			this.providers.put(getBinaryName(annotation), getBinaryName(providerImplementer));
		}
	}

	private void generateConfigFiles() {
		Filer filer = this.processingEnv.getFiler();

		for (String providerInterface : this.providers.keySet()) {
			String resourceFile = "META-INF/" + providerInterface;
			log("Working on resource file: " + resourceFile);
			try {
				SortedSet<String> allElements = Sets.newTreeSet();
				try {
					// would like to be able to print the full path
					// before we attempt to get the resource in case the behavior
					// of filer.getResource does change to match the spec, but there's
					// no good way to resolve CLASS_OUTPUT without first getting a resource.
					FileObject existingFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);
					log("Looking for existing resource file at " + existingFile.toUri());
					Set<String> oldElements = ServicesFiles.readServiceFile(existingFile.openInputStream());
					log("Existing element entries: " + oldElements);
					allElements.addAll(oldElements);
				} catch (IOException e) {
					// According to the javadoc, Filer.getResource throws an exception
					// if the file doesn't already exist. In practice this doesn't
					// appear to be the case. Filer.getResource will happily return a
					// FileObject that refers to a non-existent file but will throw
					// IOException if you try to open an input stream for it.
					log("Resource file did not already exist.");
				}

				Set<String> newServices = new HashSet<>(this.providers.get(providerInterface));
				if (allElements.containsAll(newServices)) {
					log("No new element entries being added.");
					return;
				}

				allElements.addAll(newServices);
				log("New deployment descriptor file contents: " + allElements);
				FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);
				OutputStream out = fileObject.openOutputStream();
				ServicesFiles.writeServiceFile(allElements, out);
				out.close();
				log("Wrote to: " + fileObject.toUri());
			} catch (IOException e) {
				fatalError("Unable to create " + resourceFile + ", " + e);
				return;
			}
		}
	}

	/**
	 * Returns the binary name of a reference type. For example, {@code com.google.Foo$Bar}, instead of
	 * {@code com.google.Foo.Bar}.
	 * 
	 * @param element
	 * @return The binary name
	 *
	 */
	private String getBinaryName(TypeElement element) {
		return getBinaryNameImpl(element, element.getSimpleName().toString());
	}

	private String getBinaryNameImpl(TypeElement element, String className) {
		Element enclosingElement = element.getEnclosingElement();

		if (enclosingElement instanceof PackageElement) {
			PackageElement pkg = (PackageElement) enclosingElement;
			if (pkg.isUnnamed()) {
				return className;
			}
			return pkg.getQualifiedName() + "." + className;
		}

		TypeElement typeElement = (TypeElement) enclosingElement;
		return getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
	}

	private void log(String msg) {
		if (this.processingEnv.getOptions().containsKey("debug")) {
			this.processingEnv.getMessager().printMessage(Kind.NOTE, msg);
		}
	}

	private void error(String msg, Element element, AnnotationMirror annotation) {
		this.processingEnv.getMessager().printMessage(Kind.ERROR, msg, element, annotation);
	}

	private void fatalError(String msg) {
		this.processingEnv.getMessager().printMessage(Kind.ERROR, "FATAL ERROR: " + msg);
	}
}
