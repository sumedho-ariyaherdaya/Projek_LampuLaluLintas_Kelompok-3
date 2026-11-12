package com.example.trafficlightsim.service;

import com.example.trafficlightsim.model.ViolationRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Menyimpan ViolationRecord di memori (hilang saat aplikasi ditutup). */
public class ViolationLogService {
    private final List<ViolationRecord> records = new ArrayList<>();

    public void add(ViolationRecord record) { records.add(record); }

    public List<ViolationRecord> getAll() { return Collections.unmodifiableList(records); }

    public int size() { return records.size(); }
}
