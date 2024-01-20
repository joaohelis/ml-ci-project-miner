package datamining.machinelearningprojectsminer.miner.helper;

import java.util.Date;

public class GHToken {

    private String token;
    private int remaining;
    private Date resetDate;    
    
    public GHToken(String token) {
        this.token = token;
    }

    public GHToken(String token, int remaining, Date resetDate) {
        this.token = token;
        this.remaining = remaining;
        this.resetDate = resetDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public Date getResetDate() {
        return resetDate;
    }

    public void setResetDate(Date resetDate) {
        this.resetDate = resetDate;
    }

    @Override
    public String toString() {
        return "GHToken [remaining=" + remaining + ", resetDate=" + resetDate + ", token=" + token + "]";
    }         
}