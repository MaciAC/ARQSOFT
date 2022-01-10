package com.company;

import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class NodeFactory {

    Helper help = new Helper();

    public Node buildNode(String formula, ArrayList<Node> parenthesisNodes){

        Node node;

        if(formula.charAt(0) != '(') {
            String initialSelectionOperation = help.checkSelectionOperation(formula.substring(0, min(4, formula.length())));

            //In charge of the control of "selection operations"
            if (initialSelectionOperation != "" && Objects.equals(formula.substring(formula.length() - 1), ")")) {

                node = new Node(initialSelectionOperation);
                node.children.addAll(this.Selection(formula.substring(initialSelectionOperation.length() + 1, formula.length() - 1)));
                return node;
            }
        }

        // check parenthesis to create sub tree recursively and substitute its presence in formula by hashtag
        // if parenthesis is preceeded by
        // store the subtree in NodesParenthesis which is not reliable at all maybe we should do a hash tabl

        int idxOpen = 0, idxClose = 0, j = 0, openP = 0, closeP = 0;
        ArrayList<Node> NodesParenthesis = new ArrayList<Node>();

        if(parenthesisNodes!=null)
            NodesParenthesis.addAll(parenthesisNodes);

        while(j<formula.length()){
            if(formula.charAt(j) == '(') {
                if(openP == 0)
                    idxOpen = j;
                openP++;
            }
            else if(formula.charAt(j) == ')')
                closeP++;
            if(openP == closeP && openP != 0) {
                openP = 0;
                closeP = 0;
                idxClose = j;

                // build node with interior parenthesis and add to list of nodes
                String selectionOperation = help.checkSelectionOperation(formula.substring(max(idxOpen-4,0),idxOpen));

                if(selectionOperation == "")
                    if (buildNode(formula.substring(idxOpen + 1, idxClose), null) == null)
                        return null;
                    else {
                        NodesParenthesis.add(buildNode(formula.substring(idxOpen + 1, idxClose), null));
                        formula = formula.substring(0, idxOpen) + "#" + formula.substring(idxClose + 1);
                    }

                else{
                    NodesParenthesis.add(buildNode(selectionOperation + formula.substring(idxOpen, idxClose + 1), null));
                    formula = formula.substring(0, idxOpen-selectionOperation.length()) + "#" + formula.substring(idxClose + 1);
                }



                // reset index to avoid skiping characters
                j = j - idxClose + idxOpen;

            }
            j++;
        }
        //Here below, only works with operations
        String separator = help.checkNextSeparator(formula);
        String[] children;
        if(separator!= ""){
            children = help.checkFormulaAndSplit(formula,separator);
        }
        else{
            children = new String[]{formula};
        }

        if(children.length == 0){
            return null;
        }
        int ParenthesisStored = 0;
        //Create a node without operations
        if(separator == ""){
            if(help.isNumeric(children[0])){
                node = new Node(formula);
                node.setValue(Double.parseDouble(formula));
            }
            else if(children[0].contains("#")){
                node = NodesParenthesis.get(0);
            }
            else if(help.isAddress(children[0])){
                node =  new Node(formula);
            }
            else{
                return null;
            }
        }
        else{
            separator = separator.replace("\\", "");
            node = new Node(separator);
            for(int i = 0; i<children.length;i++){
                if(Objects.equals(children[i], "#")){
                    node.children.add(NodesParenthesis.get(ParenthesisStored));
                    ParenthesisStored++;
                }else{
                    if(children[i].contains("#")){
                        if(this.buildNode(children[i],NodesParenthesis) == null){
                            return null;
                        }
                        else {
                            node.children.add(this.buildNode(children[i], NodesParenthesis));
                            ParenthesisStored++;
                        }
                    }
                    else{
                        if(this.buildNode(children[i],null) == null){
                            return null;
                        }
                        else {
                            node.children.add(this.buildNode(children[i], null));
                        }
                    }
                }
            }
        }
        return node;
    }


    public ArrayList<Node> Selection(String selection){
        ArrayList<Node> finalSelection = new ArrayList<Node>();
        ArrayList<String> elements =  help.ParseSelectionContent(selection);
        for(String element : elements){

            boolean b = element.charAt(0) != '(' && element.substring(element.length() - 1) != ")";
            boolean operation = help.checkSelectionOperation(element.substring(0, min(4, element.length()))) == "";
            if(element.contains(":") && operation && b)
                finalSelection.addAll(Range(element));
            else
                finalSelection.add(buildNode(element, null));
        }
        return finalSelection;
    }

    public ArrayList<Node> Range(String range){
        String [] address = range.split(":");
        ArrayList<Node> finalRange = new ArrayList<Node>();
        int[] init = help.fromAddressToIndex(address[0]);
        int[] last = help.fromAddressToIndex(address[1]);
        for(int i = init[0]; i<=last[0]; i++){
            for(int j = init[1]; j<=last[1];j++){
                finalRange.add(buildNode(help.fromIntToString(i) + Integer.toString(j), null));
            }
        }
        return finalRange;
    }
}
