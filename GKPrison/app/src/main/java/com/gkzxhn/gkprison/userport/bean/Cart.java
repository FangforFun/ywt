package com.gkzxhn.gkprison.userport.bean;

/**
 * Created by admin on 2015/12/22.
 */
public class Cart {
    private int id;
    private String time;
    private String out_trade_no;
    private int finish;
    private String total_money;

    public String getTotal_money() {
        return total_money;
    }

    public void setTotal_money(String total_money) {
        this.total_money = total_money;
    }

    public int getFinish() {
        return finish;
    }

    public int isFinish() {
        return finish;
    }

    public void setFinish(int finish) {
        this.finish = finish;
    }

    public String getOut_trade_no() {
        return out_trade_no;
    }

    public void setOut_trade_no(String out_trade_no) {
        this.out_trade_no = out_trade_no;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
