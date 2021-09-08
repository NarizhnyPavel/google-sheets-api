package com.app.service.domain;

import lombok.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "google_tb")
public class GoogleTable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_google")
    @SequenceGenerator(name="seq_google", sequenceName="SEQ_GOOGLE", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "table_id")
    private String spreadSheetId;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "owner")
    private String owner;

    @OneToMany(mappedBy="table")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<GoogleTableSheet> sheets;
}
