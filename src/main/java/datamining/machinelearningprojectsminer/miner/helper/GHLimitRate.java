package datamining.machinelearningprojectsminer.miner.helper;

import java.util.Date;

public class GHLimitRate {

    private int limit;
    private int used;
    private int remaining;
    private long reset;
    
    public GHLimitRate(int limit, int used, int remaining, long reset) {
        this.limit = limit;
        this.used = used;
        this.remaining = remaining;
        this.reset = reset;
    }

    public Date getResetDate(){             
        return (reset != 0)? new Date(reset) : null;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public long getReset() {
        return reset;
    }

    public void setReset(long reset) {
        this.reset = reset;
    }

    @Override
    public String toString() {
        return "GHLimitRate [limit=" + limit + ", remaining=" + remaining + ", reset=" + reset + ", used=" + used + "]";
    }
}