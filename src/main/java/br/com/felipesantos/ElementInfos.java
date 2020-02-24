package br.com.felipesantos;

import java.util.List;

/** Class used to especify elements to ignore during validation */
public class ElementInfos {

    public String tagName;
    public List<String> attributes;
    public List<String> classNames;

    /**
     * Specify tag names, attributes and classes that must be ignored in the validation
     * @param _tagName Tag name of an element, inform null if you don't want apply a filter by tag name
     * @param _attributes List of attributes that you want to be ignored, inform null if you don't want to apply a filter by attribute
     * @param _classNames List of class names that you want to be ignored, inform null if you don't want to apply a filter by class name
     */
    public ElementInfos(String _tagName, List<String> _attributes, List<String> _classNames) {
        tagName = _tagName;
        attributes = _attributes;
        classNames = _classNames;
    }
}
