package id.fatarc.portfolio.payflowreliability.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByProviderEventId(String providerEventId);
    Optional<PaymentTransaction> findByProviderTransactionId(String providerTransactionId);
    List<PaymentTransaction> findByMerchantIdAndSettledFalseAndProviderStatus(String merchantId, ProviderStatus providerStatus);
}
