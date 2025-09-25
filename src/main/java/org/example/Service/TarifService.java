package org.example.Service;

import org.example.Entities.Tarif;
import org.example.Rep.TarifRep;

import java.util.List;

public class TarifService {

    private TarifRep tarifRep;

    public TarifService(TarifRep tarifRep) {
        this.tarifRep = tarifRep;
    }

    public Tarif saveTarif(Tarif tarif) {
        return tarifRep.save(tarif);
    }

    public Tarif updateTarif(Tarif tarif) {
        return tarifRep.update(tarif);
    }

    public boolean deleteTarif(Long id) {
        return tarifRep.delete(id);
    }

    public Tarif getTarifById(Long id) {
        return tarifRep.getById(id);
    }

    public List<Tarif> getAllTarifs() {
        return tarifRep.getAll();
    }

    public Tarif getTarifByTypeService(String typeService) {
        return tarifRep.getByTypeService(typeService);
    }

    public double getMontantTarifByTypeService(String typeService) {
        Tarif tarif = tarifRep.getByTypeService(typeService);
        return tarif != null ? tarif.getMontant() : 0.0;
    }
}


