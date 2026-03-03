package tr.org.lider.dto;

import java.util.Date;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTaskDTO {

	private int pageNumber;
    private int pageSize;
    private Optional <Boolean> status;
	private Optional<String> taskCommand;
	@DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private Optional<Date> startDate;
	@DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private Optional<Date> endDate;
	private String username;
}
