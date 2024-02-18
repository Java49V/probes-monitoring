package telran.probes.service;

import telran.probes.dto.AccountDto;
//import telran.security.accounting.dto.*;

public interface AccountProviderService {
AccountDto getAccount(String email);
}
