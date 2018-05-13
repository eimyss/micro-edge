package de.eimantas.edgeservice.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.eimantas.edgeservice.client.OverviewClient;
import de.eimantas.edgeservice.dto.AccountOverView;
import de.eimantas.edgeservice.dto.ExpensesResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import de.eimantas.edgeservice.client.ExpensesClient;
import de.eimantas.edgeservice.dto.Expense;

@RestController
public class ExpensesController {
	private final ExpensesClient expensesClient;

	private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
    private OverviewClient overviewClient;

	public ExpensesController(ExpensesClient expensesClient) {
		this.expensesClient = expensesClient;
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@GetMapping("/open-expenses")
	@CrossOrigin(origins = "*")
	public Collection<Expense> openExpenses() {
		logger.info("edge expenses request");
        Collection<Expense> expenses = expensesClient.readExpenses();
		logger.info("expenses count: " + expenses.size());
		return expenses.stream().filter(this::isOpen).collect(Collectors.toList());
	}

	private boolean isOpen(Expense expenses) {
            return true;
	}


    @GetMapping("/account/overview/{id}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<AccountOverView> readOverviewForAccount(@PathVariable long id) {
        logger.info("edge account overview request");
        AccountOverView overview = overviewClient.readOverview(id);
        logger.info("expenses count: " + overview.toString());
        return new ResponseEntity(overview, HttpStatus.OK);
    }

	public Collection<Expense> fallback(Throwable e) {
		logger.warn("faLLING BACK on get expenses");
		e.printStackTrace();
		List<Expense> list = new ArrayList<>();
		list.add(new Expense());
		return list;
	}
}
