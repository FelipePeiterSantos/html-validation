package br.com.felipesantos;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** Module to compare two HTML sources, validating if they have changes against each other*/
public class HtmlValidation {
    /** White list of elements to ignore during validation */
    private List<ElementInfos> listIgnore = null;
    /** Original HTML to validate */
    private Document original = null;
    private boolean verifyElementsLength = true;
    /** List containing tags, attributes or classes not found during validation */
    private Map<String,String> notFound = new HashMap<String,String>();

    /**
     * Perform the validation, comparing two page sources against each other
     * @param pageSource Raw HTML to validate
     * @param originalPageSource Raw HTML to compare to
     * @return If "pageSource" doesn't have differences compared to "originalPageSource"
     */
    public boolean validate(String pageSource, String originalPageSource) {
        try {
            original = Jsoup.parse(originalPageSource);
            Document current = Jsoup.parse(pageSource);

            if(verifyElementsLength){
                if (current.getAllElements().size() != original.getAllElements().size()) {
                    System.out.println("Current elements' size is different from original's - Current["+current.getAllElements().size()+"] Original["+original.getAllElements().size()+"]");
                    return false;
                }
            }

            boolean isValid = true;
            for (Element element : current.getAllElements()) {
                isValid &= verifyElement(element);
            }

            return isValid;
        } catch (Exception e) {
            throw e;
        }
    }


    /**
     * Verify if a specified tag name should be ignored in validation
     * @param tagName HTML's tag name to verify
     * @return If "tagName" is on the white list
     */
    private boolean verifyIgnoreTag(String tagName) {
        if(listIgnore != null)
            for (ElementInfos ignoreInfo : listIgnore)
                if (ignoreInfo.tagName != null)
                    if (ignoreInfo.tagName.equals(tagName))
                        if (ignoreInfo.attributes == null)
                            if (ignoreInfo.classNames == null)
                                return true;
        return false;
    }


    /**
     * Verify if the element is present in the original source
     * @param currElement Html's element to validate
     * @return If element "currElement" doesn't have attribute and class differences compared with the original source
     */
    private boolean verifyElement(Element currElement) {
        if(verifyIgnoreTag(currElement.tagName()))
            return true;

        notFound = new HashMap<String,String>();
        for (Element origElement : original.getAllElements()) {
            notFound.put("TAG",currElement.tagName());
            if(currElement.tagName().equals(origElement.tagName())){
                if(verifyAllAttributes(currElement, origElement) && verifyText(currElement,origElement)){
                    notFound.remove("TAG",currElement.tagName());
                    return true;
                }
            }
        }
        System.out.println("ELEMENT NOT FOUND >> <"+currElement.tagName()+currElement.attributes()+">"+currElement.ownText());
        System.out.println("NOT FOUND >> "+notFound);
        return false;
    }

    /**
     * Verify if element's text corresponds to the source
     * @param currElement Element to compare with the "origElement" text
     * @param origElement Element to compare with the "currElement" text
     * @return If "currElement" doesn't have text differences compared to "origElement"
     */
    private boolean verifyText(Element currElement, Element origElement) {
        if(verifyIgnoreText(currElement.tagName()))
            return true;

        if(currElement.textNodes().size() == 0 || origElement.textNodes().size() == 0){
            return true;
        }

        List<String> currText = new ArrayList<String>();
        for (TextNode textNode : currElement.textNodes())
            currText.add(textNode.text());
        Collections.sort(currText);

        List<String> origText = new ArrayList<String>();
        for (TextNode textNode : origElement.textNodes())
            origText.add(textNode.text());
        Collections.sort(origText);
        if(currText.equals(origText)){
            return true;
        }
        else{
            notFound.put("TEXT",currText.toString());
            return false;
        }
    }

    /**
     * Verify if element with the specified tag name should be ignored
     * @param tag HTML's tag name
     * @return If text validation of the corresponding "tag" should be ignored
     */
    private boolean verifyIgnoreText(String tag) {
        if(listIgnore != null) {
            for (ElementInfos ignoreInfo : listIgnore) {
                if(ignoreInfo.attributes != null){
                    for (String ignoreAttr : ignoreInfo.attributes) {
                        if(ignoreAttr.equals("text()")){
                            if(ignoreInfo.tagName == null || ignoreInfo.tagName.equals(tag))
                                return true;
                            else {
                                return false;
                            }
                        }
                    }
                }
            }
        }


        return false;
    }

    /**
     * Verify if all element's attributes correspond with the source's
     * @param currElem Element to compare with the "origElem" attributes
     * @param origElem Element to compare with the "currElem" attributes
     * @return If all attributes of element "currElem" doesn't have a difference when compared with "origElem"'s
     */
    private boolean verifyAllAttributes(Element currElem, Element origElem) {
        if(currElem.attributes().size() == 0) {
            return true;
        }

        for (Attribute attr : currElem.attributes()) {
            if (!verifyAttribute(attr, origElem)) {
                return false;
            }
        }
        notFound.remove("ATTRIBUTES_SIZE",String.valueOf(currElem.attributes().size()));
        return true;
    }

    /**
     * Verify if a specified attribute corresponds with the source
     * @param currAttr Attribute to find among "origElem" attributes
     * @param origElem Element source to validate if "currAttr" is present on it
     * @return If attribute "currAttr" is located among element "origElem" attributes
     */
    private boolean verifyAttribute(Attribute currAttr, Element origElem) {
        if(verifyIgnoreAttribute(origElem.tagName(),currAttr))
            return true;

        for (Attribute origAttr : origElem.attributes()) {
            if(currAttr.getKey().equals("class") && origAttr.getKey().equals("class")){
                notFound.put("CLASS",currAttr.getKey());
                if(verifyClasses(currAttr,origAttr,origElem.tagName())){
                    notFound.remove("CLASS",currAttr.getKey());
                    return true;
                }
            }
            else {
                notFound.put("ATTRIBUTE", currAttr.getKey());
                if(currAttr.getKey().equals(origAttr.getKey())){
                    notFound.remove("ATTRIBUTE", currAttr.getKey());
                    notFound.put("ATTRIBUTE_VALUE", currAttr.getValue());
                    if(currAttr.getValue().equals(origAttr.getValue())) {
                        notFound.remove("ATTRIBUTE_VALUE", currAttr.getValue());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Verify if all classes of a specified element correspond with the source
     * @param currAttr Attribute to verify
     * @param origAttr Attribute to compare
     * @param tag Tag name of the attribute's parent to verify if validation should be ignored
     * @return If all classes of "currAttr" are found in "origAttr" class list
     */
    private boolean verifyClasses(Attribute currAttr, Attribute origAttr, String tag) {
        List<String> currClasses = Arrays.asList(currAttr.getKey().split(" "));
        List<String> origClasses = Arrays.asList(origAttr.getKey().split(" "));
        Collections.sort(currClasses);
        Collections.sort(origClasses);

        if(currClasses.equals(origClasses)){
            return true;
        }
        else{
            for (String currClass : currClasses) {
                if(!verifyClass(tag,currClass,origClasses)){
                    return false;
                }
            }
            for (String origClass : origClasses) {
                notFound.put("CLASS",origClass);
                if(!verifyClass(tag,origClass,currClasses)){
                    notFound.put("CLASS",origClass);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Verify if a specified class name is present among a list of classes, tag name is used to verify if class name should be ignored by having a specified tag name
     * @param tagName Tag name of the class' parent
     * @param verifyClass Class name of an element
     * @param listClasses List of classes to compare to
     * @return If class name "verifyClass" is found in "listClasses"
     */
    private boolean verifyClass(String tagName, String verifyClass, List<String> listClasses) {
        if(verifyIgnoreClass(tagName,verifyClass)){
            return true;
        }

        return listClasses.contains(verifyClass);
    }

    /**
     * Verify if a specified class name should be ignored
     * @param tagName Tag name of the class' parent to verify if validation should be ignored
     * @param verifyClass Class name to verify
     * @return If class name validation should be ignored
     */
    private boolean verifyIgnoreClass(String tagName, String verifyClass) {
        for (ElementInfos elementInfos : listIgnore) {
            if(elementInfos.tagName == null || elementInfos.tagName.equals(tagName)){
                if(elementInfos.classNames != null) {
                    return elementInfos.classNames.contains(verifyClass);
                }
            }
        }

        return false;
    }

    /**
     * Verify if a specified attribute should be ignored
     * @param tag Tag name of attribute's parent
     * @param attr Attribute to verify
     * @return If attribute "attr" should be ignored
     */
    private boolean verifyIgnoreAttribute(String tag, Attribute attr) {
        if(listIgnore != null) {
            for (ElementInfos ignoreInfo : listIgnore) {
                if(ignoreInfo.attributes != null){
                    for (String ignoreAttr : ignoreInfo.attributes) {
                        if(ignoreAttr.equals(attr.getKey())){
                            if(ignoreInfo.tagName == null)
                                return true;
                            else if(ignoreInfo.tagName.equals(tag))
                                return true;
                            else {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Set tags that should be ignored in validation
     * @param tags Tags to ignore
     */
    public void ignoreTags(String... tags) {
        for (String tag : tags) {
            ignoreElement(new ElementInfos(tag,null,null));
        }
    }

    /**
     * Set element with specified properties that should be ignored during validation
     * @param elementInfos Set tags, attributes and/or classes to ignore
     */
    public void ignoreElement(ElementInfos... elementInfos) {
        for (ElementInfos elemInfo : elementInfos) {
            if (listIgnore == null) {
                listIgnore = new ArrayList<ElementInfos>();
            }
            listIgnore.add(elemInfo);
        }
    }

    /**
     * Writes to and generates a HTML file containing what is informed in "pagesource"
     * @param pageSource HTML to create file from
     * @param filePath Path to create file
     */
    public void generateFileSource(String pageSource, String filePath) {
        Path path = Paths.get(filePath);
        if(!Files.exists(path.getParent())){
            try {
                Files.createDirectory(path.getParent());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        byte[] strToBytes = pageSource.getBytes();
        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Method used to return a list with all tag names contained in the HTML
     * @param pageSource HTML source
     * @return A list with all tag names contained in the HTML "pageSource"
     */
    public List<String> returnAllTags(String pageSource) {
        Document source = Jsoup.parse(pageSource);
        List<String> listTags = new ArrayList<String>();
        for (Element elem : source.getAllElements()) {
            if (!listTags.contains(elem.tagName()))
                listTags.add(elem.tagName());
        }
        return listTags;
    }

    /**
     * Method used to return a list with all attributes contained in the HTML
     * @param pageSource HTML source
     * @return A list with all attributes of the elements contained in the HTML "pageSource"
     */
    public List<String> returnAllAttributes(String pageSource) {
        Document source = Jsoup.parse(pageSource);
        List<String> listAttrbs = new ArrayList<String>();
        for (Element elem : source.getAllElements()) {
            for (Attribute attrb : elem.attributes()) {
                if (!listAttrbs.contains(attrb.getKey()))
                    listAttrbs.add(attrb.getKey());
            }
        }
        return listAttrbs;
    }

    /** Set variable "verifyElementsLength" to false, ignoring quantity of elements in the HTML*/
    public void ignoreElementsLength() {
        verifyElementsLength = false;
    }
}

