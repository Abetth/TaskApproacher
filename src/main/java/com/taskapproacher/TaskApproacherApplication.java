package com.taskapproacher;

import com.taskapproacher.dao.TaskBoardDAO;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TaskApproacherApplication {

	public static void main(String[] args) {
		TaskBoardDAO tbd = new TaskBoardDAO();
		SpringApplication.run(TaskApproacherApplication.class, args);
	}

}
