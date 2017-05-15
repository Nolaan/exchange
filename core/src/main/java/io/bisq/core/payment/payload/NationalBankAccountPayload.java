/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.core.payment.payload;

import io.bisq.generated.protobuffer.PB;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@ToString
@Slf4j
public final class NationalBankAccountPayload extends BankAccountPayload {

    public NationalBankAccountPayload(String paymentMethod, String id, long maxTradePeriod) {
        super(paymentMethod, id, maxTradePeriod);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private NationalBankAccountPayload(String paymentMethodName,
                                       String id,
                                       long maxTradePeriod,
                                       String countryCode,
                                       String holderName,
                                       String bankName,
                                       String branchId,
                                       String accountNr,
                                       String accountType,
                                       String holderTaxId,
                                       String bankId) {
        super(paymentMethodName,
                id,
                maxTradePeriod,
                countryCode,
                holderName,
                bankName,
                branchId,
                accountNr,
                accountType,
                holderTaxId,
                bankId);
    }

    @Override
    public PB.NationalBankAccountPayload toProtoMessage() {
        return getBankAccountPayloadBuilder()
                .setNationalBankAccountPayload(PB.NationalBankAccountPayload.newBuilder())
                .build()
                .getNationalBankAccountPayload();
    }

    public static NationalBankAccountPayload fromProto(PB.PaymentAccountPayload proto) {
        PB.CountryBasedPaymentAccountPayload countryBasedPaymentAccountPayload = proto.getCountryBasedPaymentAccountPayload();
        PB.BankAccountPayload bankAccountPayload = countryBasedPaymentAccountPayload.getBankAccountPayload();
        return new NationalBankAccountPayload(proto.getPaymentMethodId(),
                proto.getId(),
                proto.getMaxTradePeriod(),
                countryBasedPaymentAccountPayload.getCountryCode(),
                bankAccountPayload.getHolderName(),
                bankAccountPayload.getBankName(),
                bankAccountPayload.getBranchId(),
                bankAccountPayload.getAccountNr(),
                bankAccountPayload.getAccountType(),
                bankAccountPayload.getHolderTaxId().isEmpty() ? null : bankAccountPayload.getHolderTaxId(),
                bankAccountPayload.getBankId());
    }
    
    @Override
    public String getPaymentDetails() {
        return "National Bank transfer - " + getPaymentDetailsForTradePopup().replace("\n", ", ");
    }
}