package at.campus02.dbp2.repository;

import java.sql.*;

public class JDBCRepository implements CustomerRepository {

    private Connection connection;

    public JDBCRepository(String jdbcUrl) {
        try {
            connection = DriverManager.getConnection(jdbcUrl);

            ensureTable();
        } catch (SQLException e) {
            throw new IllegalStateException("No database connection", e);
        }
    }

    private void ensureTable() throws SQLException {

        boolean tableExists =
                connection.getMetaData().getTables(null, null, "CUSTOMER", null).next();

        if (!tableExists) {
            PreparedStatement statement = connection.prepareStatement(
                    "CREATE Table Customer (" +
                            "email varchar(50) PRIMARY KEY, " +
                            "lastname varchar(50), " +
                            "firstname varchar(50))"

            );

            statement.execute();
        }

    }

    @Override
    public void create(Customer customer) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO Customer VALUES(?,?,?)"
            );
            statement.setString(1, customer.getEmail());
            statement.setString(2, customer.getLastname());
            statement.setString(3, customer.getFirstname());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Could not insert customer", e);
        }


    }

    @Override
    public Customer read(String email) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM Customer WHERE Email = ?"
            );
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Customer fromDB = new Customer();
                fromDB.setEmail(rs.getString(1));
                fromDB.setLastname(rs.getString(2));
                fromDB.setFirstname(rs.getString(3));
                return fromDB;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read customer", e);
        }

        return null;
    }

    @Override
    public void update(Customer customer) {

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE Customer " +
                            "SET lastname = ?, firstname = ? " +
                            "WHERE email = ?"
            );
            statement.setString(1, customer.getLastname());
            statement.setString(2, customer.getFirstname());
            statement.setString(3, customer.getEmail());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException("Could not update customer.", e);
        }

    }


    @Override
    public void delete(Customer customer) {

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE from Customer " +
                            "WHERE email = ?"
            );

            statement.setString(1, customer.getEmail());
            statement.execute();

        } catch (SQLException e) {
            throw new IllegalStateException("Could not delete customer", e);
        }

    }
}
