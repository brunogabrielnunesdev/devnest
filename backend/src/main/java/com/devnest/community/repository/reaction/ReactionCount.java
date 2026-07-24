package com.devnest.community.repository.reaction;

import com.devnest.community.entity.reaction.ReactionType;

public interface ReactionCount {

	ReactionType getType();

	long getTotal();
}
