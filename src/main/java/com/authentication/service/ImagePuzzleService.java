package com.authentication.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ImagePuzzleService {

    // Maps Session ID -> Correct Ordered Grid State Array String (e.g., "0,1,2,3,4,5,6,7,8")
    private final Map<String, String> puzzleStates = new ConcurrentHashMap<>();

    public List<Integer> generateScrambledGrid(String sessionId) {
        // We will use a clean 3x3 grid layout (9 pieces total)
        List<Integer> pieces = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        
        // Shuffle the grid until it is completely unarranged
        Collections.shuffle(pieces);
        
        // Save the randomized arrangement mapping state into memory cache
        StringBuilder stateStr = new StringBuilder();
        for (int i = 0; i < pieces.size(); i++) {
            stateStr.append(pieces.indexOf(i)); // Track where piece 'i' ended up
            if (i < pieces.size() - 1) stateStr.append(",");
        }
        
        puzzleStates.put(sessionId, stateStr.toString());
        return pieces;
    }

    public boolean verifyPuzzleSolution(String sessionId, String userSolution) {
        if (sessionId == null || !puzzleStates.containsKey(sessionId) || userSolution == null) {
            return false;
        }
        
        // Evict key instantly on extraction to block repetitive replay bypass vulnerabilities
        puzzleStates.remove(sessionId);
        
        // If the user's submitted arrangement array matches perfectly, authentication passes!
        return userSolution.trim().equals("0,1,2,3,4,5,6,7,8");
    }
}

