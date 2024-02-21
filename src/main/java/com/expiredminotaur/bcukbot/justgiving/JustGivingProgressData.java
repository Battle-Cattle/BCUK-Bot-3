package com.expiredminotaur.bcukbot.justgiving;

public class JustGivingProgressData
{
    double total;
    double target;

    public double getTotal()
    {
        return total;
    }

    public void setTotal(double total)
    {
        this.total = total;
    }

    public double getTarget()
    {
        return target;
    }

    public void setTarget(double target)
    {
        this.target = target;
    }

    public double getPercentage()
    {
        return 100 * total / target;
    }

    public void setData(double total, double target)
    {
        this.total = total;
        this.target = target;
    }
}
