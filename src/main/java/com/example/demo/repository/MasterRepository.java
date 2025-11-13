package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class MasterRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== Employees ====================
    public List<Map<String, Object>> getEmployees(String teamName) {
        String sql = """
            SELECT e.id AS id,
                   e.name AS name,
                   e.designation AS designation,
                   e.salary AS salary,
                   e.team_id AS team_id,
                   t.team_name AS team_name
            FROM employees e
            LEFT JOIN teams t ON e.team_id = t.id
        """;

        if (teamName != null && !teamName.isEmpty()) {
            sql += " WHERE t.team_name = ?";
            return jdbcTemplate.queryForList(sql, teamName);
        }
        return jdbcTemplate.queryForList(sql);
    }

    public String addEmployee(Map<String, Object> body) {
        String sql = """
            INSERT INTO employees (name, designation, salary, team_id)
            VALUES (?, ?, ?, (SELECT id FROM teams WHERE team_name = ?))
        """;
        jdbcTemplate.update(sql,
                body.get("name"),
                body.get("designation"),
                body.get("salary"),
                body.get("team_name"));
        return "Employee added successfully";
    }

    public String deleteEmployee(int id) {
        jdbcTemplate.update("DELETE FROM employees WHERE id = ?", id);
        return "Employee deleted successfully";
    }

    // ==================== Products ====================
    public List<Map<String, Object>> getProducts(String teamName) {
        String sql = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.quantity AS quantity,
                   p.price AS price,
                   p.progress AS progress,
                   t.team_name AS team_name,
                   p.employee_id AS employee_id,
                   p.sale_month AS sale_month
            FROM products p
            LEFT JOIN teams t ON p.team_id = t.id
        """;

        if (teamName != null && !teamName.isEmpty()) {
            sql += " WHERE t.team_name = ?";
            return jdbcTemplate.queryForList(sql, teamName);
        }
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> addProduct(Map<String, Object> body) {
        String sql = """
            INSERT INTO products (name, quantity, price, progress, team_id, employee_id, sale_month)
            VALUES (?, ?, ?, ?, (SELECT id FROM teams WHERE team_name = ?), ?, ?)
        """;
        jdbcTemplate.update(
                sql,
                body.get("name"),
                body.get("quantity"),
                body.get("price"),
                body.getOrDefault("progress", "In Progress"),
                body.get("team_name"),
                body.get("employee_id"),
                body.getOrDefault("sale_month", null)
        );
        return getProducts(null);
    }

    public List<Map<String, Object>> updateProduct(int id, Map<String, Object> body) {
        String sql = """
            UPDATE products
            SET name = ?,
                quantity = ?,
                price = ?,
                progress = ?,
                team_id = (SELECT id FROM teams WHERE team_name = ?),
                employee_id = ?,
                sale_month = ?
            WHERE id = ?
        """;
        jdbcTemplate.update(
                sql,
                body.get("name"),
                body.get("quantity"),
                body.get("price"),
                body.getOrDefault("progress", "In Progress"),
                body.get("team_name"),
                body.get("employee_id"),
                body.getOrDefault("sale_month", null),
                id
        );
        return getProducts(null);
    }

    public String deleteProduct(int id) {
        jdbcTemplate.update("DELETE FROM products WHERE id = ?", id);
        return "Product deleted successfully";
    }

    // ==================== Revenue ====================
    public List<Map<String, Object>> getRevenue(String teamName) {
        String sql = """
            SELECT t.team_name AS team_name,
                   SUM(p.price * p.quantity) AS total_revenue
            FROM products p
            LEFT JOIN teams t ON p.team_id = t.id
        """;

        if (teamName != null && !teamName.isEmpty()) {
            sql += " WHERE t.team_name = ? GROUP BY t.team_name";
            return jdbcTemplate.queryForList(sql, teamName);
        }

        sql += " GROUP BY t.team_name";
        return jdbcTemplate.queryForList(sql);
    }

    // ==================== Teams ====================
    public List<Map<String, Object>> getTeams() {
        String sql = "SELECT id, team_name FROM teams";
        return jdbcTemplate.queryForList(sql);
    }

}
