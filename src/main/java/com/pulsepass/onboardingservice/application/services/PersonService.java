package com.pulsepass.onboardingservice.application.services;

import com.pulsepass.onboardingservice.application.dtos.output.GetInternalPersonDTO;
import com.pulsepass.onboardingservice.application.dtos.output.GetPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.NaturalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonPhoneNumberRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository personRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final PersonPhoneNumberRepository personPhoneNumberRepository;

    public PersonService(
            PersonRepository personRepository,
            NaturalPersonRepository naturalPersonRepository,
            LegalPersonRepository legalPersonRepository,
            PersonPhoneNumberRepository personPhoneNumberRepository
    ) {
        this.personRepository = personRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.personPhoneNumberRepository = personPhoneNumberRepository;
    }

    public GetPersonDTO getPerson(String personKey, String selectedAgentKey, String selectedUserKey) {
        logger.info("PersonService.getPerson - Starting");

        if (!personKey.equals(selectedAgentKey) && !personKey.equals(selectedUserKey)) {
            logger.warn("PersonService.getPerson - Person key differs from the selected agent and the selected user");
            throw new PersonNotFoundException();
        }

        Person person = personRepository
                .findByPersonKey(personKey)
                .orElseThrow(PersonNotFoundException::new);
        LegalPerson legalPerson = legalPersonRepository.findByPersonKey(personKey).orElse(null);
        NaturalPerson naturalPerson = naturalPersonRepository.findByPersonKey(personKey).orElse(null);

        logger.info("PersonService.getPerson - Successfully finished");
        return new GetPersonDTO(person, legalPerson, naturalPerson);
    }

    public GetInternalPersonDTO getInternalPerson(String personKey) {
        logger.info("PersonService.getInternalPerson - Starting");

        Person person = personRepository
                .findByPersonKey(personKey)
                .orElseThrow(PersonNotFoundException::new);
        List<PersonPhoneNumber> phoneNumbers = personPhoneNumberRepository.findAllByPersonKey(personKey);
        LegalPerson legalPerson = legalPersonRepository.findByPersonKey(personKey).orElse(null);
        NaturalPerson naturalPerson = naturalPersonRepository.findByPersonKey(personKey).orElse(null);

        logger.info("PersonService.getInternalPerson - Successfully finished");
        return new GetInternalPersonDTO(person, phoneNumbers, legalPerson, naturalPerson);
    }
}
