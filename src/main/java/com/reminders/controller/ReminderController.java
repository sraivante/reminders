package com.reminders.controller;

import com.reminders.dto.ReminderForm;
import com.reminders.model.Reminder;
import com.reminders.model.ReminderCycle;
import com.reminders.service.ReminderService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Reminder> reminders = reminderService.findAll();
        model.addAttribute("reminders", reminders);
        if (!model.containsAttribute("reminderForm")) {
            model.addAttribute("reminderForm", new ReminderForm());
        }
        model.addAttribute("cycles", Arrays.asList(ReminderCycle.values()));
        return "reminders";
    }

    @GetMapping("/reminders/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("reminderForm", reminderService.findFormById(id));
        model.addAttribute("message", "Editing reminder #" + id);
        return home(model);
    }

    @PostMapping("/reminders")
    public String createOrUpdate(
            @Valid @ModelAttribute("reminderForm") ReminderForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return home(model);
        }
        reminderService.save(form);
        redirectAttributes.addFlashAttribute("message", "Reminder saved.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/accept")
    public String accept(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reminderService.acceptAndMoveToNextPeriod(id);
        redirectAttributes.addFlashAttribute("message", "Reminder accepted and moved to next period.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reminderService.toggleActive(id);
        redirectAttributes.addFlashAttribute("message", "Reminder status updated.");
        return "redirect:/";
    }

    @PostMapping("/reminders/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reminderService.delete(id);
        redirectAttributes.addFlashAttribute("message", "Reminder deleted.");
        return "redirect:/";
    }
}
