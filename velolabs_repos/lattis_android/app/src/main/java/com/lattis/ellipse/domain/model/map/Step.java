package com.lattis.ellipse.domain.model.map;

/**
 * Created by raverat on 2/24/17.
 */

public class Step {

    private String instruction;

    public Step() {

    }

    public Step(String instruction) {
        this.instruction = instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return this.instruction;
    }

}
