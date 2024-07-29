/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

/**
 *
 * @author eroot
 */
public class BaseModel {

    protected String type;
    protected String action;
    protected int priority=0;
    private String payload="";
    private String from;
    protected long count;
    protected long counter;

    public BaseModel() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BaseModel{");
        sb.append("type=").append(type);
        sb.append(", action=").append(action);
        sb.append(", priority=").append(priority);
        sb.append(", payload=").append(payload);
        sb.append(", from=").append(from);
        sb.append(", count=").append(count);
        sb.append(", counter=").append(counter);
        sb.append('}');
        return sb.toString();
    }
    
    
}
