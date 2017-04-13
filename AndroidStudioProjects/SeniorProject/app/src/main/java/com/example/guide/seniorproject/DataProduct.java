package com.example.guide.seniorproject;

import java.io.Serializable;

/**
 * Created by guide on 3/1/2017.
 */

public class DataProduct implements Serializable{
    public String ProductImg;
    public String ProducrName;
    public Double price;
    public boolean isSelected;

    public void setProductImg(String ProductImg)
    {
        this.ProductImg = ProductImg;
    }

    public void setProducrName(String ProductName)
    {
        this.ProducrName = ProductName;
    }

    public void setPrice (Double price)
    {
        this.price = price;
    }

    public String getProductImg()
    {
        return ProductImg;
    }

    public String getProducrName()
    {
        return ProducrName;
    }

    public Double getPrice()
    {
        return price;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected){
        isSelected = selected;
    }
}

