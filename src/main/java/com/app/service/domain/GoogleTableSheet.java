package com.app.service.domain;

import com.sun.istack.NotNull;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "id")
@EqualsAndHashCode(of = "id")
@Table(name = "google_sheet_tb")
public class GoogleTableSheet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_google_sheet")
    @SequenceGenerator(name="seq_google_sheet", sequenceName="SEQ_GOOGLE_SHEET", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name="table_id")
    private GoogleTable table;

    @NotNull
    @Column(name="sheet_id")
    private int sheetId;

    @NotNull
    @Column(name="sheet_name")
    private String sheetName;

}
