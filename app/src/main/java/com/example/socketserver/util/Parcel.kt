package com.example.socketserver.util

import java.io.Serializable

class Parcel : Serializable{

    private var text : String ?= null
    private var file : ByteArray? = null
    private var fileName : String? = null
    private var pageNumber : Int? = null
    private var isYTLink = false
    private var isPdf = false
    private var isWebLink = false

    constructor(str: String, pdf: Boolean){
        if(pdf){
            this.fileName = str
            this.isPdf = pdf
        }else{
            this.text = str
        }
    }

    constructor(name: String, page: Int){
        this.fileName = name
        this.pageNumber = page
        this.isPdf = true
    }

    /*constructor(inFile: ByteArray,  name: String){
        this.file = inFile
        this.fileName = name
    }

    constructor(inFile: ByteArray,  name: String, page: Int){
        this.file = inFile
        this.fileName = name
        this.pageNumber = page
    }*/

    public fun setIsYTLink(bool: Boolean){
        isYTLink = bool
    }
    public fun isYTLink() : Boolean {
        return isYTLink
    }

    public fun setIsPdf(bool: Boolean){
        isPdf = bool
    }
    public fun isPdf() : Boolean{
        return isPdf
    }

    public fun setIsWebLink(bool: Boolean){
        isWebLink = bool
    }
    public fun isWebLink() : Boolean{
        return isWebLink
    }

    public  fun getText() : String {
        return text!!
    }

    public fun setFile(bytes: ByteArray){
        file = bytes
    }

    public fun getFile() : ByteArray {
        return file!!
    }

    public fun getFileName() : String{
        return fileName!!
    }

    public fun getPageNumber() : Int? {
        return pageNumber
    }
}