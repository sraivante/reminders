package com.reminders.controller;

import com.reminders.config.SessionKeys;
import com.reminders.dto.ReminderForm;
import com.reminders.model.Reminder;
import com.reminders.model.ReminderCycle;
import com.reminders.service.ReminderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for reminder CRUD operations exposed via the web UI.
 * Supports creating, editing, accepting, toggling, duplicating, and deleting reminders.
 * All operations are scoped to the currently authenticated user.
 */
@Controller
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        String userEmail = userEmail(session);
        List<Reminder> reminders = reminderService.findAllForOwner(userEmail);
        List<String> existingTitles = reminders.stream()
                .map(Reminder::getTitle)
                .filter(title -> title != null && !title.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("reminders", reminders);
        model.addAttribute("existingTitles", existingTitles);
        model.addAttribute("loggedInEmail", userEmail);
        if (!model.containsAttribute("reminderForm")) {
            model.addAttribute("reminderForm", new ReminderForm());
        }
        model.addAttribute("cycles", Arrays.asList(ReminderCycle.values()));
        return "reminders";
    }

    @GetMapping("/reminders/{id}/edit")
    public String edit(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("reminderForm", reminderService.findFormByIdForOwner(id, userEmail(session)));
        model.addAttribute("message", "Editing reminder #" + id);
        return home(model, session);
    }

    @PostMapping("/reminders")
    public String createOrUpdate(
            @Valid @ModelAttribute("reminderForm") ReminderForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (bindingResult.hasErrors()) {
            return home(model, session);
        }
        reminderService.save(form, userEmail(session));
        redirectAttributes.addFlashAttribute("message", "Reminder saved.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/accept")
    public String accept(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        reminderService.acceptAndMoveToNextPeriod(id, userEmail(session));
        redirectAttributes.addFlashAttribute("message", "Reminder accepted and moved to next period.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        reminderService.toggleActive(id, userEmail(session));
        redirectAttributes.addFlashAttribute("message", "Reminder status updated.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        reminderService.delete(id, userEmail(session));
        redirectAttributes.addFlashAttribute("message", "Reminder deleted.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/duplicate")
    public String duplicate(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        Reminder copy = reminderService.duplicate(id, userEmail(session));
        redirectAttributes.addFlashAttribute("message", "Reminder duplicated as #" + copy.getId() + ".");
        return "redirect:/";
    }

    private String userEmail(HttpSession session) {
        Object value = session.getAttribute(SessionKeys.USER_EMAIL);
        if (value == null) {
            throw new IllegalStateException("Missing authenticated user session.");
        }
        return value.toString();
    }
}
