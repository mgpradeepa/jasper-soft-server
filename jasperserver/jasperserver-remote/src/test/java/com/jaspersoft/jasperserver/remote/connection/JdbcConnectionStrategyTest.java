/*
* Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
* http://www.jaspersoft.com.
*
* Unless you have purchased  a commercial license agreement from Jaspersoft,
* the following license terms  apply:
*
* This program is free software: you can redistribute it and/or  modify
* it under the terms of the GNU Affero General Public License  as
* published by the Free Software Foundation, either version 3 of  the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero  General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public  License
* along with this program.&nbsp; If not, see <http://www.gnu.org/licenses/>.
*/
package com.jaspersoft.jasperserver.remote.connection;

import com.jaspersoft.jasperserver.api.common.error.handling.SecureExceptionHandler;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.JdbcDataSourceService;
import com.jaspersoft.jasperserver.api.engine.jasperreports.service.impl.JdbcReportDataSourceServiceFactory;
import com.jaspersoft.jasperserver.api.metadata.common.service.RepositoryService;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.JdbcReportDataSource;
import com.jaspersoft.jasperserver.api.metadata.jasperreports.domain.client.JdbcReportDataSourceImpl;
import com.jaspersoft.jasperserver.dto.common.ErrorDescriptor;
import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.remote.resources.converters.JdbcDataSourceResourceConverter;
import com.jaspersoft.jasperserver.remote.resources.converters.ToServerConversionOptions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.same;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

/**
 * <p></p>
 *
 * @author yaroslav.kovalchyk
 * @version $Id: JdbcConnectionStrategyTest.java 57603 2015-09-15 17:20:48Z psavushc $
 */
public class JdbcConnectionStrategyTest {
    private static ClientJdbcDataSource INITIAL_CONNECTION_DESCRIPTION = new ClientJdbcDataSource()
            .setConnectionUrl("someUrl").setUsername("someUsername").setPassword("somePassword")
            .setDriverClass("someDriverClass").setUri("/test/resource/uri");
    @InjectMocks
    private JdbcConnectionStrategy strategy = new JdbcConnectionStrategy();
    @Mock
    private MessageSource messageSource;
    @Mock
    private RepositoryService repository;
    @Mock
    private JdbcReportDataSourceServiceFactory jdbcDataSourceFactory;
    @Mock
    private JdbcDataSourceResourceConverter jdbcDataSourceResourceConverter;
    @Mock
    private JdbcDataSourceService jdbcDataSourceService;
    @Mock
    private SecureExceptionHandler secureExceptionHandlerMock;
    private ClientJdbcDataSource testConnectionDescription;


    @BeforeClass
    public void init(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void refresh() throws SQLException {
        reset(messageSource, repository, jdbcDataSourceFactory, jdbcDataSourceService, repository,
                jdbcDataSourceResourceConverter);

        testConnectionDescription = new ClientJdbcDataSource(INITIAL_CONNECTION_DESCRIPTION);

        JdbcReportDataSource serverJdbcReportDataSource = new JdbcReportDataSourceImpl();

        when(jdbcDataSourceResourceConverter.toServer(same(testConnectionDescription),
                any(ToServerConversionOptions.class))).thenReturn(serverJdbcReportDataSource);
        when(jdbcDataSourceFactory.createService(serverJdbcReportDataSource)).thenReturn(jdbcDataSourceService);
        when(jdbcDataSourceService.testConnection()).thenReturn(true);
        when(secureExceptionHandlerMock.handleException(isA(Throwable.class), isA(ErrorDescriptor.class))).thenReturn(new ErrorDescriptor().setMessage("test"));
    }

    @Test
    public void createConnection_withPassword_passed() throws Exception {
        final ClientJdbcDataSource result = strategy.createConnection(testConnectionDescription, null);
        assertSame(result, testConnectionDescription);
    }

    @Test
    public void createConnection_passwordSubstitution_passed() throws Exception{
        final String passwordSubstitution = "passwordSubstitution";
        doReturn(passwordSubstitution).when(messageSource).getMessage(eq("input.password.substitution"), isNull(Object[].class), any(Locale.class));
        JdbcReportDataSource dataSource = new JdbcReportDataSourceImpl();
        final String expectedPassword = "expectedPassword";
        dataSource.setPassword(expectedPassword);
        doReturn(dataSource).when(repository).getResource(null, INITIAL_CONNECTION_DESCRIPTION.getUri());
        final ClientJdbcDataSource result = strategy.createConnection(testConnectionDescription.setPassword(passwordSubstitution), null);
        assertSame(result, testConnectionDescription);
        assertEquals(result.getPassword(), passwordSubstitution);
    }

    @Test
    public void createConnection_passwordNull_passed() throws Exception{
        JdbcReportDataSource dataSource = new JdbcReportDataSourceImpl();
        final String expectedPassword = "expectedPassword";
        dataSource.setPassword(expectedPassword);
        doReturn(dataSource).when(repository).getResource(null, INITIAL_CONNECTION_DESCRIPTION.getUri());
        final ClientJdbcDataSource result = strategy.createConnection(testConnectionDescription.setPassword(null), null);
        assertSame(result, testConnectionDescription);
        assertNull(result.getPassword());
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void createConnection_connectionIsNull_exception()throws Exception{
        when(jdbcDataSourceService.testConnection()).thenReturn(false);
        strategy.createConnection(testConnectionDescription, null);
    }

    @Test(expectedExceptions = ConnectionFailedException.class)
    public void createConnection_connectionCreatingThrowsException_exception()throws Exception{
        doThrow(new RuntimeException()).when(jdbcDataSourceService).testConnection();
        strategy.createConnection(testConnectionDescription, null);
    }

}
