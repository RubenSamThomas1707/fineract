/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SavingsAccountsApiResourceTest {

    private SavingsAccountsApiResource underTest;
    private final String BIRTHDAY_AND_BIRTHMONTH_ERROR_MESSAGE = "birthMonth and birthDay must be provided together";
    private final String BIRTHDAY_RANGE_ERROR_MESSAGE = "birthDay must be between 1 and 31";
    private final String BIRTHMONTH_RANGE_ERROR_MESSAGE = "birthMonth must be between 1 and 12";

    @BeforeEach
    public void setUp() {
        SavingsAccountReadPlatformService mockSavingsAccountReadPlatformService = mock(SavingsAccountReadPlatformService.class);
        PlatformSecurityContext mockContext = mock(PlatformSecurityContext.class);
        DefaultToApiJsonSerializer<SavingsAccountData> mockApiJsonSerializer = mock(DefaultToApiJsonSerializer.class);
        PortfolioCommandSourceWritePlatformService mockCommandSourceWritePlatformService = mock(PortfolioCommandSourceWritePlatformService.class);
        ApiRequestParameterHelper mockApiRequestParameterHelper = mock(ApiRequestParameterHelper.class);
        SavingsAccountChargeReadPlatformService mockSavingsAccountChargeReadPlatformService = mock(SavingsAccountChargeReadPlatformService.class);
        BulkImportWorkbookService mockBulkImportWorkbookService = mock(BulkImportWorkbookService.class);
        BulkImportWorkbookPopulatorService mockBulkImportWorkbookPopulatorService = mock(BulkImportWorkbookPopulatorService.class);

        AppUser appUser = mock(AppUser.class);
        when(mockContext.authenticatedUser()).thenReturn(appUser);

        underTest = new SavingsAccountsApiResource(mockSavingsAccountReadPlatformService, mockContext, mockApiJsonSerializer,
                mockCommandSourceWritePlatformService, mockApiRequestParameterHelper,
                mockSavingsAccountChargeReadPlatformService, mockBulkImportWorkbookService,
                mockBulkImportWorkbookPopulatorService);
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenOnlyBirthDayIsProvided() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, null, 31, 0, 10, null, null));

        List<ApiParameterError> errors = ex.getErrors();
        assertEquals(1, errors.size());
        assertEquals(BIRTHDAY_AND_BIRTHMONTH_ERROR_MESSAGE, errors.get(0).getDefaultUserMessage());
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenOnlyBirthMOnthIsProvided() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, 05, null, null, null, null, null));

        List<ApiParameterError> errors = ex.getErrors();
        assertEquals(1, errors.size());
        assertEquals(BIRTHDAY_AND_BIRTHMONTH_ERROR_MESSAGE, errors.get(0).getDefaultUserMessage());
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenProvidedBirthMonthIsLessThanOne() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, -1, 31, null, null, null, null));

        assertEquals(BIRTHMONTH_RANGE_ERROR_MESSAGE, ex.getErrors().get(0).getDefaultUserMessage());
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenProvidedBirthMonthIsAboveTwelve() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, 13, 31, null, null, null, null));

        assertEquals(BIRTHMONTH_RANGE_ERROR_MESSAGE, ex.getErrors().get(0).getDefaultUserMessage());
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenProvidedBirthDayIsBelowOne() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, 05, -1, null, null, null, null));

        assertEquals(BIRTHDAY_RANGE_ERROR_MESSAGE, ex.getErrors().get(0).getDefaultUserMessage());
    }

    @Test
    public void retrieveAllThrowsParameterErrorWhenProvidedBirthDayIsAboveThirtyOne() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.retrieveAll(null, null, null, 05, 32, null, null, null, null));

        assertEquals(BIRTHDAY_RANGE_ERROR_MESSAGE, ex.getErrors().get(0).getDefaultUserMessage());
    }
}