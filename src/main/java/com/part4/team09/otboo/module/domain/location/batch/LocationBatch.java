package com.part4.team09.otboo.module.domain.location.batch;

import com.part4.team09.otboo.module.domain.location.dto.response.TLocation;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.repository.LocationRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class LocationBatch {

  @Bean
  public FlatFileItemReader<TLocation> locationItemReader() {
    FlatFileItemReader<TLocation> reader = new FlatFileItemReader<>();
    reader.setResource(new ClassPathResource("location.csv"));
    reader.setLinesToSkip(1);
    reader.setEncoding("UTF-8");

    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setDelimiter(",");
    tokenizer.setStrict(false);

    DefaultLineMapper<TLocation> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(tokenizer);
    lineMapper.setFieldSetMapper(new TLocationFieldSetMapper());

    reader.setLineMapper(lineMapper);
    return reader;
  }

  @Bean
  public ItemWriter<Location> locationItemWriter(LocationRepository repository) {
    return items -> repository.saveAll(items);
  }

  @Bean
  public Step locationStep(
    JobRepository jobRepository,
    PlatformTransactionManager transactionManager,
    FlatFileItemReader<TLocation> locationItemReader,
    TLocationProcessor locationProcessor,
    ItemWriter<Location> locationItemWriter
  ) {
    return new StepBuilder("locationStep", jobRepository)
      .<TLocation, Location>chunk(100, transactionManager)
      .reader(locationItemReader)
      .processor(locationProcessor)
      .writer(locationItemWriter)
      .build();
  }

  @Bean("locationJob")
  public Job locationImportJob(JobRepository jobRepository, Step locationStep) {
    return new JobBuilder("locationImportJob", jobRepository)
      .start(locationStep)
      .build();
  }
}
