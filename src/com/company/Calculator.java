package com.company;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Calculator {

    Spreadsheet spreadsheet = new HashSpreadsheet();
    Helper help = new Helper();
    ArrayList<String> address_passed;



    public void run(Spreadsheet sp){
        spreadsheet = sp;
        Set<String> formulas = spreadsheet.GetFormulas();
        int non_computed = formulas.size();
        for(String address : formulas) {
            address_passed = new ArrayList<String>();
            computeTree(address);
        }
    }

    public Tree computeTree(String address){
        CellFormula c = (CellFormula) spreadsheet.GetCell(address);
        if(address_passed.contains(address)){
            c.tree.root = new Node("Error, loop among addresses!");
        }
        else {
            address_passed.add(address);
            c.tree.root = computeNode(c.tree.root);
            if(c.tree.root.value == null){
                c.tree.root = new Node("0.0");
                c.tree.root.value = 0.0;
            }
            spreadsheet.SetResult(address, c.tree);
        }
        return c.tree;
    }

    public Node computeNode(Node node){
        if(help.isNumeric(node.content))
            return node;
        else if(help.isAddress(node.content)){
            Cell c = spreadsheet.GetCell(node.content);
            if(c == null){
                return null;
            }
            if(c instanceof CellFormula){
                CellFormula cf = (CellFormula) c;
                if(cf.value == null){
                    cf.tree = computeTree(node.content);
                    return cf.tree.root;
                }
                else
                    return cf.tree.root;
            }
            else {
                Node n = new Node(c.content);
                n.setValue(Double.parseDouble(c.content));
                return n;
            }
        }
        Node n_final = returnOperation(node);
        return n_final;
    }

    public Node returnOperation(Node node){
        Node number;
        ArrayList<Double> valuesToOperate = new ArrayList<Double>();
        for(Node child : node.children){
            number = this.computeNode(child);
            if(number != null){
                if(number.content == "Error, loop among addresses!")
                    return number;
                else
                    valuesToOperate.add(number.value);
            }
        }
        if(valuesToOperate.size() == 0) {
            return node;
        }
        switch (node.content){
            case "+":
                node.value = this.sumArrayList(valuesToOperate);
                break;
            case "-":
                node.value = this.substractArrayList(valuesToOperate);
                break;
            case "*":
                node.value = this.productArrayList(valuesToOperate);
                break;
            case "/":
                node.value = this.divisionArrayList(valuesToOperate);
                break;
            case "SUM":
                node.value = this.sumArrayList(valuesToOperate);
                break;
            case "MAX":
                node.value = this.maxArrayList(valuesToOperate);
                break;
            case "MIN":
                node.value = this.minArrayList(valuesToOperate);
                break;
            case "MEAN":
                node.value = this.promedArrayList(valuesToOperate);
                break;
        }
        return node;
    }

    public double sumArrayList(ArrayList<Double> arrayNums){
        double result = 0;
        for(Double num : arrayNums)
            result = result + num;

        return result;
    }

    public double substractArrayList(ArrayList<Double> arrayNums){
        double result = arrayNums.get(0);
        for(int i = 1; i<arrayNums.size(); i++)
            result = result-arrayNums.get(i);

        return result;
    }
    public double productArrayList(ArrayList<Double> arrayNums){
        double result = arrayNums.get(0);
        for(int i = 1; i<arrayNums.size(); i++)
            result = result*arrayNums.get(i);

        return result;
    }
    public double divisionArrayList(ArrayList<Double> arrayNums){
        double result = arrayNums.get(0);
        for(int i = 1; i<arrayNums.size(); i++)
            result = result/arrayNums.get(i);

        return result;
    }

    public double maxArrayList(ArrayList<Double> arrayNums){
        double result = NEGATIVE_INFINITY;
        for(Double max : arrayNums){
            if(max>result)
                result = max;
        }
        return result;
    }

    public double minArrayList(ArrayList<Double> arrayNums){
        double result = POSITIVE_INFINITY;
        for(Double min : arrayNums){
            if(min<result)
                result = min;
        }
        return result;
    }

    public double promedArrayList(ArrayList<Double> arrayNums){
        return this.sumArrayList(arrayNums)/(arrayNums.size());
    }








}
