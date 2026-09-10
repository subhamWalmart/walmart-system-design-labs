package com.training.retailorderhub.repository;

import com.training.retailorderhub.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // findById(), save(), delete() etc. come from JpaRepository.
    //
    // TRAINING NOTE (Day 2, ISP reflection):
    // Keep this interface limited to data access. If a reporting or accounting
    // capability is ever needed (e.g. generateInvoiceReport(), exportToAccountingSystem()),
    // it belongs on its own interface (InvoiceReporter, AccountingExporter) so that
    // clients which only need findById()/save() aren't forced to depend on methods
    // they never call. See Lab 1's ISP reflection question.
}
