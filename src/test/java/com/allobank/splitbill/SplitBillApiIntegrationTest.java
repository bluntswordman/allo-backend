package com.allobank.splitbill;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SplitBillApiIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createsGroupExpenseAndSettlement() throws Exception {
		CreatedGroup group = createGroup("Bali Trip", "Bedy", "Ani", "Doni");

		String expenseBody = """
				{
				  "description": "Makan malam",
				  "amount": 100000,
				  "paid_by_participant_id": "%s",
				  "beneficiary_participant_ids": ["%s", "%s", "%s"]
				}
				""".formatted(group.participantIds().get(0), group.participantIds().get(0),
				group.participantIds().get(1), group.participantIds().get(2));

		mockMvc.perform(post("/api/v1/groups/{groupId}/expenses", group.id())
					.contentType(MediaType.APPLICATION_JSON)
					.content(expenseBody))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.amount").value(100000))
				.andExpect(jsonPath("$.shares[0].amount").value(33334))
				.andExpect(jsonPath("$.shares[1].amount").value(33333))
				.andExpect(jsonPath("$.shares[2].amount").value(33333));

		mockMvc.perform(get("/api/v1/groups/{groupId}/settlements", group.id()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total_expenses").value(100000))
				.andExpect(jsonPath("$.service_charge_pct").value(4))
				.andExpect(jsonPath("$.service_charge_amount").value(4000))
				.andExpect(jsonPath("$.settlements.length()").value(2));
	}

	@Test
	void rejectsDuplicateParticipantNamesIgnoringCase() throws Exception {
		mockMvc.perform(post("/api/v1/groups")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "name": "Duplicate test",
							  "participants": [{"name": "Bedy"}, {"name": " bedy "}]
							}
							"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid request"));
	}

	@Test
	void rejectsParticipantFromAnotherGroup() throws Exception {
		CreatedGroup first = createGroup("First group", "A", "B");
		CreatedGroup second = createGroup("Second group", "C", "D");

		String body = """
				{
				  "description": "Invalid member",
				  "amount": 100,
				  "paid_by_participant_id": "%s",
				  "beneficiary_participant_ids": ["%s"]
				}
				""".formatted(second.participantIds().get(0), first.participantIds().get(0));

		mockMvc.perform(post("/api/v1/groups/{groupId}/expenses", first.id())
					.contentType(MediaType.APPLICATION_JSON)
					.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Payer must be a participant of the requested group"));
	}

	@Test
	void rejectsDuplicateBeneficiaryIds() throws Exception {
		CreatedGroup group = createGroup("Duplicate beneficiaries", "A", "B");
		UUID participantId = group.participantIds().get(0);
		String body = """
				{
				  "description": "Duplicate beneficiary",
				  "amount": 100,
				  "paid_by_participant_id": "%s",
				  "beneficiary_participant_ids": ["%s", "%s"]
				}
				""".formatted(participantId, participantId, participantId);

		mockMvc.perform(post("/api/v1/groups/{groupId}/expenses", group.id())
					.contentType(MediaType.APPLICATION_JSON)
					.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Beneficiary participant IDs must be unique"));
	}

	@Test
	void returnsNotFoundForUnknownGroup() throws Exception {
		mockMvc.perform(get("/api/v1/groups/{groupId}/settlements", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Resource not found"));
	}

	private CreatedGroup createGroup(String groupName, String firstName, String secondName, String... otherNames)
			throws Exception {
		StringBuilder participants = new StringBuilder()
				.append("{\"name\":\"").append(firstName).append("\"},")
				.append("{\"name\":\"").append(secondName).append("\"}");
		for (String name : otherNames) {
			participants.append(", {\"name\":\"").append(name).append("\"}");
		}
		String body = "{\"name\":\"" + groupName + "\",\"participants\":[" + participants + "]}";

		String response = mockMvc.perform(post("/api/v1/groups")
					.contentType(MediaType.APPLICATION_JSON)
					.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String groupId = JsonPath.read(response, "$.id");
		List<String> participantIds = JsonPath.read(response, "$.participants[*].id");
		return new CreatedGroup(UUID.fromString(groupId), participantIds.stream().map(UUID::fromString).toList());
	}

	private record CreatedGroup(UUID id, List<UUID> participantIds) {
	}
}
