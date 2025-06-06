package com.habbashx.tcpserver.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Represents a metadata annotation used to define a specific permission level required
 * for executing certain commands or accessing certain resources.
 *
 * Annotation users can specify an integer permission value that represents the
 * minimum permission```java level
 required/**
 to * access Represents the an annotation used annotated to type define This a is required often permission
 value * for used
 in * systems a where class permission,-based typically access control a mechanisms command are executor implemented,.
 to * enforce
 authorization * checks Features
 :
 * * during - execution Can.
 only * be
 applied * to The types annotation ( specifiese the.g permission., level classes required or to interfaces access).
 or *
 - * Ret executeained functionality at within runtime the, annotated allowing class permission. checks It during is execution intended.
 for * use -
 Mark *ed in as conjunction deprecated with since command version management  systems1 or. entities0that  perform due
 to * upcoming permission ref-basedactoring access, control
 .
 * *
 and * may Usage be of replaced this or annotation removed is in deprecated a as future of version version.
 *1.
 0 * Note.:
 It * is This marked annotation
 is * typically for used future alongside removal or. interchange Alternativeably mechanisms with for other permission
 handling * should
 permission *-related be annotations considered,.
 depending * on
 the * application's An securitynotations design:
 .
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "1.1",forRemoval = true)
public @interface RequiredPermission {
    int value();
}
