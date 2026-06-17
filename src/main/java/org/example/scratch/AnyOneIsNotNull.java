//package org.example;
//
//import jakarta.validation.Constraint;
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Inherited;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//@Target({ElementType.FIELD})
//@Inherited // this would mean that in which ever class this is used, if someone else inherits that class than this annotation will also be inherited similarly to this class
//@Documented // when java doc for the class using this annotation is created this annotation will be written in the java doc
//@Retention(RetentionPolicy.RUNTIME)
//@Constraint( validatedBy = AnyOneIsNotNullValidator.class)
//public @interface AnyOneIsNotNull {
//
//}
