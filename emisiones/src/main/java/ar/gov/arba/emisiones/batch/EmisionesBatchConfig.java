package ar.gov.arba.emisiones.batch;

import ar.gov.arba.emisiones.model.DeudaAutomotor;
import ar.gov.arba.emisiones.repository.DeudaAutomotorRepository;
import ar.gov.arba.emisiones.service.EmailService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Map;

@Configuration
public class EmisionesBatchConfig {

    private final DeudaAutomotorRepository repository;
    private final EmailService emailService;

    public EmisionesBatchConfig(DeudaAutomotorRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Bean
    public ItemReader<DeudaAutomotor> reader() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7); // Próximas 7 días

        return new RepositoryItemReaderBuilder<DeudaAutomotor>()
                .name("deudaReader")
                .repository(repository)
                .methodName("findDeudasProximasAVencer")
                .arguments(startDate, endDate)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<DeudaAutomotor, DeudaAutomotor> processor() {
        return deuda -> {
            // Aquí se puede agregar lógica adicional, como filtrar o transformar
            return deuda;
        };
    }

    @Bean
    public ItemWriter<DeudaAutomotor> writer() {
        return items -> {
            for (DeudaAutomotor deuda : items) {
                emailService.enviarNotificacion(deuda);
            }
        };
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("enviarNotificacionesStep", jobRepository)
                .<DeudaAutomotor, DeudaAutomotor>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, Step step) {
        return new JobBuilder("emisionesJob", jobRepository)
                .start(step)
                .build();
    }
}