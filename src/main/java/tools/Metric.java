/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package tools;

import java.time.LocalDate;

/**
 *
 * @author endeleya
 */
public record Metric(LocalDate period,double chiffreAffaire,
        double coutAchat,double result,String region) {}


