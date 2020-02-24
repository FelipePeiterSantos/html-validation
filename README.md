# HTML-Validation

HTML-Validation is a Java library to validate changes in HTML compared to a previous HTML source.

## Installation

You can use [Maven](https://maven.apache.org/) to install HTML-Validation in your repository.
* Download this project
* Navigate to where you downloaded the project
* Execute the command below:
```bash
mvn install
```
* Add to your project's pom.xml the following dependency:
```xml
<dependency>
   <groupId>br.com.felipesantos</groupId>
   <artifactId>html-validation</artifactId>
   <version>0.1</version>
<dependency>
```
## Usage

```java
//Instantiate the variable
HtmlValidation htmlValidation = new HtmlValidation();


//Generate a .html file with the informed html content
htmlValidation.generateFileSource("RAW HTML","PATH TO SAVE FILE");


//Return a list containing all tags from the HTML source
htmlValidation.returnAllTags("HTML Source");


//Return a list containing all attributes from the HTML source
htmlValidation.returnAllAttributes("HTML Source");


//Call this method to ignore validation if quantity of elements matches with the source's
htmlValidation.ignoreElementsLength();


//Inform tags you want to ignore in the validation
htmlValidation.ignoreTags("body", "script", "div");


//Inform tags, attribute and/or classes you want to ignore in the validation
htmlValidation.ignoreElement(
   //Element information to be ignored
   new ElementInfos("input", Arrays.asList("value","id"), null),
   new ElementInfos(null, Arrays.asList("id"), null)
);


//Perform validation
htmlValidation.validate("RAW HTML TO VALIDATE", "RAW HTML TO COMPARE WITH");

```
More information in the Javadoc.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.