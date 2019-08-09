

JCharset - Java Charset package 1.5
===================================

Copyright © 2005-2012 Amichai Rothman



1. What is the Java Charset package?

    The Java Charset package is an open-source implementation of character
    sets that were missing from the standard Java platform.


2. How do I use the Java Charset package?

    The Java Charset package is written in pure Java, runs on JDK 1.5 or later,
    and requires no special installation - just add jcharset.jar to your
    classpath, or place it in any of the usual extension directories.

    The JVM will recognize the supported character sets automatically, and they
    will be available anywhere character sets are used in the Java platform.

    As an example, you can take a look at java.lang.String's constructor and
    getBytes() method, both of which have an overloaded version that receives
    a charset name as an argument.

    Note: Some web/mail containers run each application in its own JVM context.
    In this case check the container documentation for information on where and
    how to configure the classpath, such as in WEB-INF/lib, shared/lib,
    jre/lib/ext, etc. You may need to restart the server for changes to take
    effect. However, if you use Sun's JRE, it will work only if you put it in
    the jre/lib/ext extension directory, or in the container's classpath.
    This is due to a bug in Sun's JRE implementation
    (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4619777).
    Voting for the bug may expedite its fix, so please do...


3. Which charsets are supported?

    "UTF-7" (a.k.a. "UTF7", "UNICODE-1-1-UTF-7", "csUnicode11UTF7",
                    "UNICODE-2-0-UTF-7")
        The 7-bit Unicode character encoding defined in RFC 2152.
        The O-set characters are encoded as a shift sequence.
        Both O-set flavors (direct and shifted) are decoded.

    "UTF-7-OPTIONAL" (a.k.a. "UTF-7O", "UTF7O", "UTF-7-O")
        The 7-bit Unicode character encoding defined in RFC 2152.
        The O-set characters are directly encoded.
        Both O-set flavors (direct and shifted) are decoded.

    "SCGSM" (a.k.a. "GSM-default-alphabet", "GSM_0338", "GSM_DEFAULT",
                    "GSM7", "GSM-7BIT")
        The GSM default charset as specified in GSM 03.38, used in SMPP for
        encoding SMS text messages.

        Additional flavors of the GSM charset are "CCGSM", "SCPGSM" and
        "CCPGSM": The CC prefix signifies mapping the Latin capital letter C
        with cedilla character, the SC prefix signifies mapping the Latin small
        letter c with cedilla character, and the P prefix signifies the packed
        form (8 characters packed in 7 bytes), as specified by the spec.
        See javadocs for details.

    "hp-roman8" (a.k.a. "roman8", "r8", "csHPRoman8", "X-roman8")
        The HP Roman-8 charset, as provided in RFC 1345.

    "ISO-8859-8-BIDI" (a.k.a. "csISO88598I", "ISO-8859-8-I", "ISO_8859-8-I",
                              "csISO88598E", "ISO-8859-8-E", "ISO_8859-8-E")
        The ISO 8859-8 charset implementation exists in the standard JRE.
        However, it is lacking the i/e aliases, which specify whether
        bidirectionality is implicit or explicit. The charsets conversions
        themselves are similar. This charset complements the standard one.

    "ISO-8859-6-BIDI" (a.k.a. "csISO88596I", "ISO-8859-6-I", "ISO_8859-6-I",
                              "csISO88596E", "ISO-8859-6-E", "ISO_8859-6-E")
        The ISO 8859-6 charset implementation exists in the standard JRE.
        However, it is lacking the i/e aliases, which specify whether
        bidirectionality is implicit or explicit. The charsets conversions
        themselves are similar. This charset complements the standard one.

    "KOI8-U" (a.k.a. "KOI8-RU", "KOI8_U")
        The KOI8-U Ukrainian charset, as defined in RFC 2319.

    "MIK"
         The MIK cyrillic code page, commonly used by DOS applications
         in Bulgaria.


4. License

    The Java Charset package is provided under the GNU General Public
    License agreement. Please read the full license agreement in the
    included LICENSE.txt file.

    For non-GPL commercial licensing please contact the address below.


5. Contact

    Please write to support@freeutils.net with any bugs, suggestions, fixes,
    contributions, or just to drop a good word and let me know you've found
    this package useful and you'd like it to keep being maintained.

    Updates and additional info can be found at
    http://www.freeutils.net/source/jcharset/
