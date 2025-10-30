package com.shodh.backend.config;

import com.shodh.backend.model.*;
import com.shodh.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing sample data...");
        
        // Create sample contest
        Contest contest1 = Contest.builder()
            .title("Weekly Coding Contest #1")
            .description("Test your programming skills with these challenging problems!")
            .startTime(LocalDateTime.now().minusHours(1))
            .endTime(LocalDateTime.now().plusHours(2))
            .build();
        contest1 = contestRepository.save(contest1);

        // Create Problem 1: Two Sum
        Problem problem1 = Problem.builder()
            .title("Two Sum")
            .description("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\n" +
                        "You may assume that each input would have exactly one solution, and you may not use the same element twice.\n\n" +
                        "You can return the answer in any order.")
            .constraints("2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9")
            .sampleInput("4 9\n2 7 11 15")
            .sampleOutput("0 1")
            .timeLimit(1)
            .memoryLimit(256)
            .points(100)
            .contest(contest1)
            .build();
        problem1 = problemRepository.save(problem1);

        // Add test cases for Problem 1
        TestCase tc1_1 = TestCase.builder()
            .input("4 9\n2 7 11 15")
            .expectedOutput("0 1")
            .isSample(true)
            .problem(problem1)
            .build();
        testCaseRepository.save(tc1_1);

        TestCase tc1_2 = TestCase.builder()
            .input("3 6\n3 2 4")
            .expectedOutput("1 2")
            .isSample(false)
            .problem(problem1)
            .build();
        testCaseRepository.save(tc1_2);

        TestCase tc1_3 = TestCase.builder()
            .input("2 6\n3 3")
            .expectedOutput("0 1")
            .isSample(false)
            .problem(problem1)
            .build();
        testCaseRepository.save(tc1_3);

        // Create Problem 2: Palindrome Number
        Problem problem2 = Problem.builder()
            .title("Palindrome Number")
            .description("Given an integer x, return true if x is a palindrome, and false otherwise.\n\n" +
                        "An integer is a palindrome when it reads the same backward as forward.\n\n" +
                        "For example, 121 is a palindrome while 123 is not.")
            .constraints("-2^31 <= x <= 2^31 - 1")
            .sampleInput("121")
            .sampleOutput("true")
            .timeLimit(1)
            .memoryLimit(256)
            .points(150)
            .contest(contest1)
            .build();
        problem2 = problemRepository.save(problem2);

        // Add test cases for Problem 2
        TestCase tc2_1 = TestCase.builder()
            .input("121")
            .expectedOutput("true")
            .isSample(true)
            .problem(problem2)
            .build();
        testCaseRepository.save(tc2_1);

        TestCase tc2_2 = TestCase.builder()
            .input("-121")
            .expectedOutput("false")
            .isSample(true)
            .problem(problem2)
            .build();
        testCaseRepository.save(tc2_2);

        TestCase tc2_3 = TestCase.builder()
            .input("10")
            .expectedOutput("false")
            .isSample(false)
            .problem(problem2)
            .build();
        testCaseRepository.save(tc2_3);

        // Create Problem 3: FizzBuzz
        Problem problem3 = Problem.builder()
            .title("FizzBuzz")
            .description("Write a program that prints the numbers from 1 to n.\n" +
                        "But for multiples of three print \"Fizz\" instead of the number and for the multiples of five print \"Buzz\".\n" +
                        "For numbers which are multiples of both three and five print \"FizzBuzz\".")
            .constraints("1 <= n <= 100")
            .sampleInput("5")
            .sampleOutput("1\n2\nFizz\n4\nBuzz")
            .timeLimit(1)
            .memoryLimit(256)
            .points(100)
            .contest(contest1)
            .build();
        problem3 = problemRepository.save(problem3);

        // Add test cases for Problem 3
        TestCase tc3_1 = TestCase.builder()
            .input("5")
            .expectedOutput("1\n2\nFizz\n4\nBuzz")
            .isSample(true)
            .problem(problem3)
            .build();
        testCaseRepository.save(tc3_1);

        TestCase tc3_2 = TestCase.builder()
            .input("15")
            .expectedOutput("1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz")
            .isSample(false)
            .problem(problem3)
            .build();
        testCaseRepository.save(tc3_2);

        // Create a second contest for future
        Contest contest2 = Contest.builder()
            .title("Monthly Challenge Contest")
            .description("Advanced problems for experienced programmers!")
            .startTime(LocalDateTime.now().plusDays(7))
            .endTime(LocalDateTime.now().plusDays(7).plusHours(3))
            .build();
        contest2 = contestRepository.save(contest2);

        // Create a problem for contest 2
        Problem problem4 = Problem.builder()
            .title("Reverse Integer")
            .description("Given a signed 32-bit integer x, return x with its digits reversed.\n" +
                        "If reversing x causes the value to go outside the signed 32-bit integer range [-2^31, 2^31 - 1], then return 0.")
            .constraints("-2^31 <= x <= 2^31 - 1")
            .sampleInput("123")
            .sampleOutput("321")
            .timeLimit(1)
            .memoryLimit(256)
            .points(200)
            .contest(contest2)
            .build();
        problem4 = problemRepository.save(problem4);

        // Add test cases for Problem 4
        TestCase tc4_1 = TestCase.builder()
            .input("123")
            .expectedOutput("321")
            .isSample(true)
            .problem(problem4)
            .build();
        testCaseRepository.save(tc4_1);

        TestCase tc4_2 = TestCase.builder()
            .input("-123")
            .expectedOutput("-321")
            .isSample(true)
            .problem(problem4)
            .build();
        testCaseRepository.save(tc4_2);

        // Create some sample users
        User user1 = User.builder()
            .username("alice")
            .build();
        userRepository.save(user1);

        User user2 = User.builder()
            .username("bob")
            .build();
        userRepository.save(user2);

        User user3 = User.builder()
            .username("charlie")
            .build();
        userRepository.save(user3);

        log.info("Sample data initialized successfully!");
        log.info("Contest 1 ID: {} (Active now)", contest1.getId());
        log.info("Contest 2 ID: {} (Starts in 7 days)", contest2.getId());
        log.info("Created {} problems with test cases", 4);
        log.info("Created {} sample users", 3);
    }
}
