package eu.scape_project.dataconnetor.doms;

import dk.statsbiblioteket.util.Pair;
import eu.scape_project.model.Identified;
import eu.scape_project.model.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtils {
    public static <T extends Identified> List<Pair<T, T>> findInBoth(List<T> first, List<T> second) {
        List<Pair<T, T>> result = new ArrayList<>();
        Map<Identifier, T> secondCopy = new HashMap<>();
        for (T t : second) {
            secondCopy.put(t.getIdentifier(), t);
        }
        ArrayList<T> firstCopy = new ArrayList<>(first);
        firstCopy.addAll(first);
        firstCopy.retainAll(second);
        for (T t : firstCopy) {
            result.add(new Pair<>(t, secondCopy.get(t.getIdentifier())));
        }
        return result;

    }

    public static <T extends Identified> List<T> findInFirstNotInSecond(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>();
        result.addAll(first);
        result.removeAll(second);
        return result;
    }

    public static List<String> combine(List<String> first, List<String> second) {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(first);
        result.addAll(second);
        return result;
    }
}
