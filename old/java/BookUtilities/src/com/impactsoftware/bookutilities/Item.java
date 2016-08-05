package com.impactsoftware.bookutilities;

import java.text.DateFormat;

import java.text.ParseException;

import java.util.Date;

/**
 * Contains data and methods associated with a single catalog item.
 *
 * @Author:  Bruce Kissinger, 2012
 */
public class Item {
    String ID;
    String title;
    String creator;
    String language;
    String subject;    
    String dateCreated;
    String webPage;
    Double downloads;

    public Item() {
        super();
    }


    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEscaped() {
        return escapeValues(this.title);
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatorEscaped() {

        return escapeValues(this.creator);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public String getLanguageEscaped() {
        return escapeValues(language);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectEscaped() {
        return escapeValues(this.subject);
    }



    public String toString() {
        return "ID: " + getID() + ", Title: " + getTitle() + ", Creator: " +
            getCreator() + ", Language: " + getLanguage() + ", Subject: " +
            getSubject() + ", Date created: " + getDateCreated() + ", Web page: " + getWebPage();
    }

    /** This function returns string values that are correctly escaped for insertion into a database.
     */
    public String escapeValues(String aString) {
        if (aString == null) {
            return null;
        } else {
            String temp1String = aString.replaceAll("\'", "");
            String finalString = temp1String.replaceAll(",", "");
            return "'" + finalString + "'";
        }
    }

    
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateCreated() {
        return dateCreated;
    }
    
    public Date getDateCreatedAsDate() {
        Date tempDate = null;
        
        if (this.dateCreated != null && ! "None".equals(this.dateCreated)) {
            try {
              tempDate = DateFormat.getDateInstance().parse(this.dateCreated);
            } catch (ParseException ex) {
              ;;    
            }
        }
        
        return tempDate;
    }

    public String getDateCreatedEscaped() {
        return escapeValues(dateCreated);
    }


    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public String getWebPage() {
        return webPage;
    }

    public String getWebPageEscaped() {
        return escapeValues(webPage);
    }
    
    public void setDownloads(double downloads) {
        this.downloads = new Double(downloads);
    }
    
    public Double getDownloads() {
        return this.downloads;
    }
    
}
