package com.teralogic.useradminlayouts.security.services;

import com.teralogic.useradminlayouts.models.Layout;
import com.teralogic.useradminlayouts.repository.LayoutRepository;
import com.teralogic.useradminlayouts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LayoutServiceImpl implements LayoutService {

    @Autowired
    private LayoutRepository layoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Layout> getAllLayouts() {
        return layoutRepository.findAll();
    }

}