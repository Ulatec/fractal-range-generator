package com.example.fractalrangecalculator.Controller;

import com.example.fractalrangecalculator.Fetchers.StockRangeTester;
import com.example.fractalrangecalculator.Model.Bar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
public class APIController {
    @Value("${com.example.fractalrangecalculator.polygon.apiKey}")
    private String apiKey;
    @GetMapping("/getFractalHistory/{ticker}")
    public List<Bar> getFractalHistory(@PathVariable String ticker) throws Exception {
        StockRangeTester stockRangeTester = new StockRangeTester();
        stockRangeTester.setApiKey(apiKey);
        return stockRangeTester.run(ticker);
    }


}