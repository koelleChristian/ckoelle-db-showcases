package de.koelle.christian.spring.r2dbc.ratings.shared.consts;

public final class SqlStatementIds {

    public static final String DROP_RATING_PUBLICATION = "DROP TABLE IF EXISTS rating_publication;";
    public static final String DROP_RATING_METRIC = "DROP TABLE IF EXISTS rating_metric;";
    public static final String DROP_RATING_RESULT = "DROP TABLE IF EXISTS rating_result;";

    public static final String CREATE_RATING_PUBLICATION =
        """
            CREATE TABLE rating_publication (
                id INT(20) AUTO_INCREMENT,
                domain VARCHAR(100) NOT NULL,
                year INT NOT NULL,
                business_version VARCHAR(100) NOT NULL,
                publication_time TIMESTAMP NOT NULL,
                PRIMARY KEY (id),
                INDEX idx_publication_domain (domain),
                INDEX idx_publication_businessversion (business_version),
                UNIQUE KEY uk_publication_domain_year_businessversion (domain, year, business_version)
            )
                ENGINE = InnoDB
                CHARACTER SET utf8mb4
                COLLATE utf8mb4_unicode_ci;
            """;
    public static final String CREATE_RATING_METRIC =
        """
            CREATE TABLE rating_metric (
                id INT(20) AUTO_INCREMENT,
                fk_publication_id INT(20) NOT NULL,
                metric_number VARCHAR(100) NOT NULL,
                calculation_type VARCHAR(100) NOT NULL,
                PRIMARY KEY (id),
                INDEX idx_metric_metricnumber (metric_number),
                CONSTRAINT fk_metric_2_publication_id
                    FOREIGN KEY (fk_publication_id)
                        REFERENCES rating_publication (id),
                UNIQUE KEY uk_metric_fkpublicationid_metricnumber (fk_publication_id, metric_number)
            )
                ENGINE = InnoDB
                CHARACTER SET utf8mb4
                COLLATE utf8mb4_unicode_ci;
            """;
    public static final String CREATE_RATING_RESULT =
        """
            CREATE TABLE rating_result (
                id INT(20) AUTO_INCREMENT,
                fk_publication_id INT(20) NOT NULL,
                fk_metric_id INT(20) NOT NULL,
                result_reference VARCHAR(100) NOT NULL,
                result_value DECIMAL(18,8) NOT NULL,
                trend VARCHAR(100) NOT NULL,
                PRIMARY KEY (id),
                INDEX idx_result_resultreference (result_reference),
                CONSTRAINT fk_rating_2_publication_id
                    FOREIGN KEY (fk_publication_id)
                        REFERENCES rating_publication (id),
                CONSTRAINT fk_rating_2_metric_id
                    FOREIGN KEY (fk_metric_id)
                        REFERENCES rating_metric (id),
                UNIQUE KEY uk_result_fkpublicationid_fkmetricid_resultreference (fk_publication_id, fk_metric_id, result_reference)
                                            
            )
                ENGINE = InnoDB
                CHARACTER SET utf8mb4
                COLLATE utf8mb4_unicode_ci;
            """;

    private SqlStatementIds(){
    	// intentionally blank
    }
}
