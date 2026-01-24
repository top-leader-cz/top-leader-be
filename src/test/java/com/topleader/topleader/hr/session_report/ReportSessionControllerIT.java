package com.topleader.topleader.hr.session_report;

import com.topleader.topleader.IntegrationTest;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql("/sql/report/report-session.sql")
class ReportSessionControllerIT extends IntegrationTest {

    @Test
    @WithUserDetails(value = "hr1")
    void fetchReportSession() throws Exception {
            mvc.perform(post("/api/latest/report-sessions?page=0&size=10&sort=username,asc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                            "from": "2023-08-14T11:30:00Z"
                                          }
                                    """
                            ))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(content().json("""
              {
           "content": [
             {
               "username": "client1",
               "firstName": "Cool",
               "lastName": "Client1",
               "attended": 1,
               "booked": 1
             },
             {
               "username": "client3",
               "firstName": "No",
               "lastName": "Client3",
               "attended": 0,
               "booked": 1
             },
             {
               "username": "client4",
               "firstName": "No",
               "lastName": "Client4",
               "attended": 0,
               "booked": 0
             },
             {
               "username": "hr1",
               "firstName": "first",
               "lastName": "hr",
               "attended": 0,
               "booked": 0
             }
           ],
           "pageable": {
             "pageNumber": 0,
             "pageSize": 10,
             "sort": {
               "empty": false,
               "sorted": true,
               "unsorted": false
             },
             "offset": 0,
             "paged": true,
             "unpaged": false
           },
           "last": true,
           "totalElements": 4,
           "totalPages": 1,
           "first": true,
           "size": 10,
           "number": 0,
           "sort": {
             "empty": false,
             "sorted": true,
             "unsorted": false
           },
           "numberOfElements": 4,
           "empty": false
         }
        """, false));

        }

}
