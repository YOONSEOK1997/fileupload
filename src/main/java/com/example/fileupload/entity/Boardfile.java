package com.example.fileupload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "boardfile")
public class Boardfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fno")
    private int fno;

    @Column(name = "ftype")
    private String ftype;

    @Column(name = "foriginname")
    private String foriginname;

    @Column(name = "fname")
    private String fname;

    @Column(name = "fext")
    private String fext;

    @Column(name = "fsize")
    private long fsize;;

    // 자식에서 부모로 단방향 관계 설정
    //불필요한 연관관계는 조인등으로 인해 
	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "bno") // FK private Board board;
	 */
    @Column(name = "bno")
    private int bno;
}
