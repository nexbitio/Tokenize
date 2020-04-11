# Tokenize for Java (and any JVM-based language)

## Installation
```
With Gradle:
repositories {
  maven { url 'https://jitpack.io' }
}
dependencies {
  implementation 'xyz.bowser65:Tokenize:...'
}

With Maven:
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependency>
  <groupId>xyz.bowser65</groupId>
  <artifactId>Tokenize</artifactId>
  <version>...</version>
</dependency>
```

## How to use it
```java
public class Main {
    public static void main(final String[] args) {
        final Tokenize tokenize = new Tokenize("very secure private key".getBytes());

        // Generation
        final Token token = tokenize.generateToken(account);
        System.out.println(token); // xxxxxxxx.xxxxxxxxxxx.xxxxxxxxx

        // Validation
        String rawToken = "xxxxxxxx.xxxxxxxxxxx.xxxxxxxxx";
        Token token;
        try {
            token = tokenize.validateToken(rawToken, id -> Database.fetchAccount(id));
        } catch(final SecurityException e) {
            System.out.println("Invalid token signature!");
        }

        // Get an OTP key
        final OTPKey key = OTPKey.builder().name("Key Name").issuer("Issuer name").build();
        System.out.println(key.getKey()); // You just need to store this in your database
        System.out.println(key.getGoogleURI()); // Throw this in a QR code

        // Validate an OTP code
        System.out.println(OTPUtils.validateTotp("013370", "xxxxxxxxxxxxxxxx"));
        System.out.println(OTPUtils.validateHotp("013370", "xxxxxxxxxxxxxxxx", 1));
    }
}
```

## License
Like all Tokenize implementation, this implementation is released under the BSD-3-Clause license.
