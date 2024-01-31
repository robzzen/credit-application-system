package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CreditDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.CustomerServiceTest.Companion.buildCustomer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest{
    @Autowired private lateinit var creditRepository: CreditRepository
    @Autowired private lateinit var testEntityManager: TestEntityManager
    @Autowired private lateinit var customerRepository: CustomerRepository
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var objectMapper: ObjectMapper

    private lateinit var credit1: Credit
    private lateinit var credit2: Credit

    companion object{
        const val URL: String = "/api/credits"
    }

    @BeforeEach fun setup() {
        customerRepository.deleteAll()
        //val customer = buildCustomer()
        val customer = testEntityManager.persist(buildCustomer())
        credit1 = testEntityManager.persist(buildCredit(customer = customer))
        credit2 = testEntityManager.persist(buildCredit(customer = customer))
        //customerRepository.save(customer)
        val customerId = customer.id ?: throw IllegalStateException("Customer ID cannot be null")

        // Crie alguns créditos para o cliente
        val creditDto1 = builderCreditDto(customerId = customerId)
        val creditDto2 = builderCreditDto(customerId = customerId)

        val valueAsString1 = objectMapper.writeValueAsString(creditDto1)
        val valueAsString2 = objectMapper.writeValueAsString(creditDto2)

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString1))
            .andExpect(MockMvcResultMatchers.status().isCreated)
        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString2))
            .andExpect(MockMvcResultMatchers.status().isCreated)
    }
    @AfterEach fun tearDown() = customerRepository.deleteAll()

    @Test //fun saveCredit()
    fun `should create a credit and return 201 status`() {
        //given

        val creditDto: CreditDto = builderCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        // when
       mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString))
            .andExpect(MockMvcResultMatchers.status().isCreated)

        mockMvc.perform(MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"creditValue\":100.0," +
                    "\"dayFirstOfInstallment\":\"2024-03-30" +
                    "\",\"numberOfInstallments\":15," +
                    "\"customerId\":1}"))
            .andDo(MockMvcResultHandlers.print())
    // then
    }

    @Test //fun findAllByCustomerId()
    fun `should find all credit by customer id`() {
        // given
        val customerId: Long = 1L

        // when
        val creditList: List<Credit> = creditRepository.findAllByCustomerId(customerId)

        // then
        Assertions.assertThat(creditList).isNotEmpty
        Assertions.assertThat(creditList.size).isEqualTo(2)
        Assertions.assertThat(creditList.map { it.customer?.id }).allMatch { it == customerId }
    }

    @Test//fun findByCreditCode()
    fun `should find credit by credit code`(){
        //given
        val creditCode1 = UUID.fromString("aa547c0f-9a6a-451f-8c89-afddce916a29")
        val creditCode2 = UUID.fromString("49f740be-46a7-449b-84e7-ff5b7986d7ef")
        credit1.creditCode = creditCode1
        credit2.creditCode = creditCode2
        //when
        val fakeCredit1: Credit = creditRepository.findByCreditCode(creditCode1)!!
        val fakeCredit2: Credit = creditRepository.findByCreditCode(creditCode2)!!
        //then
        Assertions.assertThat(fakeCredit1).isNotNull
        Assertions.assertThat(fakeCredit2).isNotNull
        Assertions.assertThat(fakeCredit1).isSameAs(credit1)
        Assertions.assertThat(fakeCredit2).isSameAs(credit2)
    }

    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(100.0),
        dayFirstOfInstallment: LocalDate = LocalDate.now().plusMonths(2L),
        numberOfInstallments: Int = 15,
        customerId: Long = 1L
    ) = CreditDto (
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstOfInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )

    private fun buildCredit(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.of(2023, Month.APRIL, 22),
        numberOfInstallments: Int = 5,
        customer: Customer
    ): Credit = Credit(
        creditValue = creditValue,
        dayFirstInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customer = customer
    )
}