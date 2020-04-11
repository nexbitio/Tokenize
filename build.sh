# NodeJS
cd nodejs
pnpm i
pnpm run lint
pnpm run test
pnpm run jsdoc
cd ..

# Java
cd java
gradle test
gradle javadoc
cd ..

# Move everything
rm -rf docs
mkdir docs
mv nodejs/jsdoc docs/nodejs
mv java/build/docs/javadoc docs/java
