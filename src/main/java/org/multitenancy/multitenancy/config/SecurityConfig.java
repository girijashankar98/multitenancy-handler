/**
 * @author girija
 *
 */
package org.multitenancy.multitenancy.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.multitenancy.multitenancy.tenant.TenantMongoDbFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter
{
		@Value("${spring.data.mongodb.host}") private String mongoDbHost;
		@Value("${spring.data.mongodb.port}") private String mongoDbPort;
		@Value("${spring.data.mongodb.database}") private String mongoDb;
		@Value("${opa.url}") private String opaurl;

		@Autowired
		AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

		@Override
		public void configure (WebSecurity web) throws Exception
		{
			web
			.ignoring()
			.mvcMatchers("/manager/**","/v1/public/**", "/swagger-ui/**", "/v3/api-docs/**");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception
		{

			//@formatter:off
			http.authorizeRequests().anyRequest().authenticated()
					.and()
					.oauth2ResourceServer(o -> o.authenticationManagerResolver(this.authenticationManagerResolver))
					.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

			http.csrf().disable();
			//@formatter:on
		}

	@Bean
	@Primary
    TenantMongoDbFactory TenantMongoDbFactory(){
		final ConnectionString connectionString = new ConnectionString("mongodb://"+mongoDbHost+":"+mongoDbPort+"/"+mongoDb);
		final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
				.applyConnectionString(connectionString).uuidRepresentation(UuidRepresentation.STANDARD).build();
		return new TenantMongoDbFactory(MongoClients.create(mongoClientSettings),mongoDb);
	}

}
